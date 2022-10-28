package io.github.lanicc.flat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.lanicc.flat.model.Ticket;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
